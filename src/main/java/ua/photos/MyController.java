package ua.photos;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/")
public class MyController {
    private static final int ITEMS_PER_PAGE = 10;
    private Map<Long, byte[]> photos = new HashMap<Long, byte[]>();

    @RequestMapping("/")
    public String onIndex() {
        return "index";
    }

    @RequestMapping(value = "/add_photo", method = RequestMethod.POST)
    public String onAddPhoto(Model model, @RequestParam MultipartFile photo) {
        if (photo.isEmpty())
            throw new PhotoErrorException();
        try {
            long id = System.currentTimeMillis();
            photos.put(id, photo.getBytes());
            model.addAttribute("photo_id", id);
            return "result";
        } catch (IOException e) {
            throw new PhotoErrorException();
        }
    }

    @RequestMapping("/photo/{photo_id}")
    public ResponseEntity<byte[]> onPhoto(@PathVariable("photo_id") long id) {
        return photoById(id);
    }

    @RequestMapping(value = "/view", method = RequestMethod.POST)
    public ResponseEntity<byte[]> onView(@RequestParam("photo_id") long id) {
        return photoById(id);
    }

    @RequestMapping("/delete/{photo_id}")
    public String onDelete(@PathVariable("photo_id") long id) {
        if (photos.remove(id) == null)
            throw new PhotoNotFoundException();
        else
            return "index";
    }

    @ResponseBody
    @PostMapping(value = "/download_photos", produces = "application/zip")
    public void download(@RequestParam(value = "toDelete[]", required = false) long[] toDownload, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.addHeader("Content-Disposition", "attachment; filename=\"photos.zip\"");
        ZipOutputStream zipOutputStream = new ZipOutputStream(resp.getOutputStream());
        // create a list to add files to be zipped
        ArrayList<File> files = new ArrayList<>();
        for (long id : toDownload) {
            File file = new File("id-" + String.valueOf(id));
            file.createNewFile();
            FileOutputStream f = new FileOutputStream(file.getName());
            f.write(photos.get(id));
            files.add(file);
        }
        // package files
        for (File f : files) {
            //new zip entry and copying inputstream with f to zipOutputStream, after all closing streams
            zipOutputStream.putNextEntry(new ZipEntry(f.getName()));
            FileInputStream fileInputStream = new FileInputStream(f);
            IOUtils.copy(fileInputStream, zipOutputStream);
            fileInputStream.close();
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();
    }

    @PostMapping("/photos/delete")
    public ResponseEntity<Void> delete(@RequestParam(value = "toDelete[]", required = false) long[] toDelete) {
        if (toDelete != null && toDelete.length > 0) {
            for (int i = 0; i < toDelete.length; i += 1) {
                photos.remove(toDelete[i]);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/view_all")
    public String getAllPhoto(Model model) {
        model.addAttribute("photos", photos.keySet());
        return "index";
    }

    private ResponseEntity<byte[]> photoById(long id) {
        byte[] bytes = photos.get(id);
        if (bytes == null)
            throw new PhotoNotFoundException();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
    }
}